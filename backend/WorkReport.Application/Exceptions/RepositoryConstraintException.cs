namespace WorkReport.Application.Exceptions;

public sealed class RepositoryConstraintException(RepositoryConstraint constraint) : Exception
{
    public RepositoryConstraint Constraint { get; } = constraint;
}

public enum RepositoryConstraint
{
    Unique,
    ForeignKey
}
