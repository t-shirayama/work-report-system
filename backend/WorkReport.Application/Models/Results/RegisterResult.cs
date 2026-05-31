namespace WorkReport.Application.Models.Results;

public sealed record RegisterResult(int? WorkReportId, IReadOnlyList<string> Errors);
