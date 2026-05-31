namespace WorkReport.Domain.Codes;

public static class WorkCategory
{
    public const string Design = "DESIGN";
    public const string Development = "DEVELOPMENT";
    public const string Test = "TEST";
    public const string Meeting = "MEETING";
    public const string Document = "DOCUMENT";
    public const string Other = "OTHER";

    private static readonly HashSet<string> SupportedCodes = new(StringComparer.Ordinal)
    {
        Design, Development, Test, Meeting, Document, Other
    };

    public static bool IsSupported(string? code)
        => !string.IsNullOrWhiteSpace(code) && SupportedCodes.Contains(code);
}
