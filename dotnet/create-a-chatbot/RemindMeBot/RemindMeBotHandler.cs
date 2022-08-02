using JetBrains.Space.AspNetCore.Experimental.WebHooks;
using JetBrains.Space.Client;

namespace RemindMeBot;

// ReSharper disable once ClassNeverInstantiated.Global
public class RemindMeBotHandler
    : SpaceWebHookHandler
{
    private readonly ChatClient _chatClient;

    public RemindMeBotHandler(ChatClient chatClient)
    {
        _chatClient = chatClient;
    }

    public override async Task<Commands> HandleListCommandsAsync(ListCommandsPayload payload)
    {
        return new Commands(new List<CommandDetail>
        {
            new CommandDetail("help", "Show this help"),
            new CommandDetail("remind", "Remind me in N seconds, e.g., to remind in 10 seconds, send 'remind 10'")
        });
    }

    public override async Task HandleMessageAsync(MessagePayload payload)
    {
        var messageText = payload.Message.Body as ChatMessageText;
        if (string.IsNullOrEmpty(messageText?.Text)) return;

        if (messageText.Text.Trim().StartsWith("remind"))
        {
            await HandleRemindAsync(payload, messageText);
            return;
        }
            
        await HandleHelpAsync(payload);
    }

    private async Task HandleRemindAsync(MessagePayload payload, ChatMessageText messageText)
    {
        var arguments = messageText.Text.Split(' ', StringSplitOptions.TrimEntries);
        if (arguments.Length != 2 
            || !int.TryParse(arguments[1], out var delayInSeconds))
        {
            // We're expecting 2 elements: "remind", "X"
            // If that's not the case, return help.
            await HandleHelpAsync(payload);
            return;
        }
            
        await _chatClient.Messages.SendMessageAsync(
            recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
            content: ChatMessage.Block(
                outline: new MessageOutline($"I will remind you in {delayInSeconds} seconds", new ApiIcon("smile")),
                sections: new List<MessageSectionElement>()));
            
        Task.Run(async () =>
        {
            try
            {
                await Task.Delay(TimeSpan.FromSeconds(delayInSeconds));
                    
                await _chatClient.Messages.SendMessageAsync(
                    recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
                    content: ChatMessage.Block(
                        outline: new MessageOutline($"Hey! {delayInSeconds} seconds are over!", new ApiIcon("smile")),
                        sections: new List<MessageSectionElement>()));
            }
            catch (Exception)
            {
                // Since we're using Task.Run to run code outside of the
                // request context, we want to catch any Exception here
                // to prevent the server from crashing.
            }
        });
    }

    private async Task HandleHelpAsync(MessagePayload payload)
    {
        var commands = await HandleListCommandsAsync(
            new ListCommandsPayload { UserId = payload.UserId });
            
        await _chatClient.Messages.SendMessageAsync(
            recipient: MessageRecipient.Member(ProfileIdentifier.Id(payload.UserId)),
            content: ChatMessage.Block(
                outline: new MessageOutline("Remind me bot help", new ApiIcon("smile")),
                sections: new List<MessageSectionElement>
                {
                    MessageSectionElement.MessageSection(
                        header: "List of available commands",
                        elements: new List<MessageElement>
                        {
                            MessageElement.MessageFields(
                                commands.CommandsItems
                                    .Select(it => MessageFieldElement.MessageField(it.Name, it.Description))
                                    .ToList<MessageFieldElement>())
                        })
                }));
    }
}