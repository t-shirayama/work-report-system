namespace WorkReport.IntegrationTests.Support;

[CollectionDefinition(Name, DisableParallelization = true)]
public sealed class IntegrationTestCollection : ICollectionFixture<WorkReportApiFixture>
{
    public const string Name = "integration-tests";
}
