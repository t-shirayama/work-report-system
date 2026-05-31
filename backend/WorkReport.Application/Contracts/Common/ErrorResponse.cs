namespace WorkReport.Application.Contracts;

public sealed record ErrorResponse(IReadOnlyList<string> Errors);
