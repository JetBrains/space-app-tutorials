using System.ComponentModel.DataAnnotations;

namespace SpaceTranslate.Database;

public class User
{
    [Key] 
    public int Id { get; set; }
        
    public DateTimeOffset Created { get; set; }
    
    public int OrganizationId { get; set; }
    public Organization Organization { get; set; }

    [Required]
    public string UserId { get; set; } = default!;

    [MaxLength(Int32.MaxValue)]
    public string? Scope { get; set; }
    public string? RefreshToken { get; set; }
}