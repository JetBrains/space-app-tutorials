using System.Security.Cryptography;
using System.Text;

namespace SpaceTranslate.WebHook;

public static class StringExtensions
{
    private static readonly MD5 Md5Helper = MD5.Create();
    
    public static string ToMd5(this string current)
    {
        var inputBytes = Encoding.ASCII.GetBytes(current);
        var hashBytes = Md5Helper.ComputeHash(inputBytes);

        var sb = new StringBuilder();
        for (var i = 0; i < hashBytes.Length; i++)
        {
            sb.Append(hashBytes[i].ToString("X2"));
        }
        return sb.ToString();
    }
}