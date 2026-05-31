namespace WorkReport.Application.Masters;

public static class RoleCode
{
    private static readonly HashSet<string> SupportedCodes = new(StringComparer.Ordinal)
    {
        "ADMIN", "USER"
    };

    public static bool IsSupported(string? code)
        => !string.IsNullOrWhiteSpace(code) && SupportedCodes.Contains(code);
}
