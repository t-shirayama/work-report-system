using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration;

namespace WorkReport.Infrastructure.Persistence;

public sealed class SqlConnectionFactory(IConfiguration configuration)
{
    public SqlConnection Create()
    {
        var connectionString = configuration.GetConnectionString("WorkReport")
            ?? throw new InvalidOperationException("ConnectionStrings:WorkReport is required.");
        return new SqlConnection(connectionString);
    }
}
