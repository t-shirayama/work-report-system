using Microsoft.Data.SqlClient;

namespace WorkReport.Api.Infrastructure;

public sealed class SqlConnectionFactory(IConfiguration configuration)
{
    public SqlConnection Create()
    {
        var connectionString = configuration.GetConnectionString("WorkReport")
            ?? throw new InvalidOperationException("ConnectionStrings:WorkReport is required.");
        return new SqlConnection(connectionString);
    }
}
