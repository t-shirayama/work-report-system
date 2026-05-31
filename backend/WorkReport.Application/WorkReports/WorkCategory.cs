namespace WorkReport.Application.WorkReports;

public static class WorkCategory
{
    private static readonly HashSet<string> SupportedCodes = new(StringComparer.Ordinal)
    {
        "DESIGN", "DEVELOPMENT", "TEST", "MEETING", "DOCUMENT", "OTHER"
    };

    public static bool IsSupported(string? code)
        => !string.IsNullOrWhiteSpace(code) && SupportedCodes.Contains(code);
}
