using System.ComponentModel.DataAnnotations;

namespace SpaceTranslate.Database;

public class Organization
{
    [Key] 
    public int Id { get; set; }
        
    public DateTimeOffset Created { get; set; }
    
    public List<User> Users { get; set; }

    [Required]
    public string ServerUrl { get; set; } = default!;

    [Required]
    public string ClientId { get; set; } = default!;

    [Required]
    public string ClientSecret { get; set; } = default!;

    [Required]
    public string UserId { get; set; } = default!;

    [Required]
    public string SigningKey { get; set; } = default!;
}