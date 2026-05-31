namespace WorkReport.Application.Models.Results;

public sealed record MasterResult<T>(T? Value, IReadOnlyList<string> Errors, bool Missing)
{
    public static MasterResult<T> Succeeded(T value)
        => new(value, [], false);

    public static MasterResult<T> Failed(IReadOnlyList<string> errors)
        => new(default, errors, false);

    public static MasterResult<T> NotFound()
        => new(default, [], true);
}
