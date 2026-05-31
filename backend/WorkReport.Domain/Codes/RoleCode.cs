namespace WorkReport.Domain.Codes;

public static class RoleCode
{
    public const string Admin = "ADMIN";
    public const string User = "USER";

    private static readonly HashSet<string> SupportedCodes = new(StringComparer.Ordinal)
    {
        Admin, User
    };

    public static bool IsSupported(string? code)
        => !string.IsNullOrWhiteSpace(code) && SupportedCodes.Contains(code);

    public static bool IsAdmin(string? code)
        => code == Admin;
}
