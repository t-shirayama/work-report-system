using Microsoft.Extensions.Options;
using WorkReport.Application.Interfaces;

namespace WorkReport.Infrastructure.Reporting;

public sealed class LocalReportFileStorage(IOptions<ReportStorageOptions> options) : IReportFileStorage
{
    public async Task<string> SaveAsync(string relativePath, byte[] content)
    {
        var basePath = options.Value.BasePath ?? "generated-reports";
        var fullPath = Path.Combine(basePath, relativePath);
        Directory.CreateDirectory(Path.GetDirectoryName(fullPath)!);
        await File.WriteAllBytesAsync(fullPath, content);
        return Path.Combine(basePath, relativePath).Replace('\\', '/');
    }

    public async Task<byte[]?> ReadAsync(string storedPath)
    {
        if (!File.Exists(storedPath))
        {
            return null;
        }

        return await File.ReadAllBytesAsync(storedPath);
    }
}
