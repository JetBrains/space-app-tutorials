using JetBrains.Space.Client;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;

namespace SpaceTranslate.WebHook;

public partial class SpaceTranslateWebHookHandler
{
    public override async Task<Commands> HandleListCommandsAsync(ListCommandsPayload payload)
    {
        using var loggerScopeForClientId = _logger.BeginScope("ClientId={ClientId}", payload.ClientId);
        
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist. ClientId={ClientId}", payload.ClientId);
            return new Commands();
        }
        
        return new Commands(
            new List<CommandDetail>
            {
                new("translate", "Translate a message to English")
            });
    }

    public override async Task HandleMessageAsync(MessagePayload payload)
    {
        using var loggerScopeForClientId = _logger.BeginScope("ClientId={ClientId}", payload.ClientId);
        
        var organization = await _db.Organizations.FirstOrDefaultAsync(it => it.ClientId == payload.ClientId);
        if (organization == null)
        {
            _logger.LogWarning("The organization does not exist. ClientId={ClientId}", payload.ClientId);
            return;
        }
        
        if (payload.Message.Body is not ChatMessageText messageText || string.IsNullOrEmpty(messageText.Text))
        {
            _logger.LogWarning("Unknown payload message body type. MessageBodyType={MessageBodyType}", payload.Message.Body?.GetType());
            return;
        }

        var messageTextBody = messageText.Text.StartsWith("translate ", StringComparison.OrdinalIgnoreCase)
            ? messageText.Text.Substring("translate ".Length)
            : messageText.Text;
        
        var organizationConnection = organization.CreateConnection();
        var organizationChatClient = new ChatClient(organizationConnection);
        
        var cacheKey = organization.Id + "__" +
                       messageTextBody.ToMd5();
    
        var cachedTranslation = await _cache.GetOrCreateAsync(cacheKey, async _ =>
        {
            if (string.IsNullOrWhiteSpace(messageTextBody)) return "(empty)";
        
            _logger.LogInformation("Requesting translation from DeepL...");
            
            var translatedText = await _translator.TranslateTextAsync(
                text: messageTextBody,
                sourceLanguageCode: null,
                targetLanguageCode: "en-US",
                options: null);
        
            _logger.LogInformation("Received translation from DeepL. DetectedSourceLanguageCode={DetectedSourceLanguageCode}", translatedText.DetectedSourceLanguageCode);
    
            return translatedText.Text;
        });
    
        if (cachedTranslation != null)
        {
            await organizationChatClient.Messages.SendMessageAsync(
                recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
                content: ChatMessage.Block(new List<MessageSectionElement>
                {
                    MessageSectionElement.MessageSection(new List<MessageBlockElement>
                    {
                        MessageBlockElement.MessageText($"Translation of message: "),
                        MessageBlockElement.MessageDivider(),
                        MessageBlockElement.MessageText(cachedTranslation)
                    })
                }));
        }
        else
        {
            _logger.LogWarning("Could not translate message");
            
            await organizationChatClient.Messages.SendMessageAsync(
                recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
                content: ChatMessage.Block(new List<MessageSectionElement>
                {
                    MessageSectionElement.MessageSection(new List<MessageBlockElement>
                    {
                        MessageBlockElement.MessageText("Could not translate message")
                    })
                }));
            
            return;
        }
        
        await base.HandleMessageAsync(payload);
    }
}