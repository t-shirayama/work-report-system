namespace WorkReport.Application.Interfaces;

public interface IReportFileStorage
{
    Task<string> SaveAsync(string relativePath, byte[] content);

    Task<byte[]?> ReadAsync(string storedPath);
}
