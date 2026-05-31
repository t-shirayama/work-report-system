using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class MasterDataIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task Admin_CanCreateAndUpdateDepartmentsAndUsers()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var departmentResponse = await api.PostJsonWithCsrfAsync("/api/master/departments", new
        {
            departmentCode = "ops",
            departmentName = "運用部",
            displayOrder = 9
        });
        var departmentJson = await api.ReadJsonAsync(departmentResponse);
        var departmentId = departmentJson.GetProperty("departmentId").GetInt32();

        Assert.Equal(HttpStatusCode.Created, departmentResponse.StatusCode);
        Assert.Equal("OPS", departmentJson.GetProperty("departmentCode").GetString());

        var updateDepartmentResponse = await api.PutJsonWithCsrfAsync($"/api/master/departments/{departmentId}", new
        {
            departmentCode = "OPS2",
            departmentName = "運用改善部",
            displayOrder = 10
        });

        Assert.Equal(HttpStatusCode.OK, updateDepartmentResponse.StatusCode);

        var userResponse = await api.PostJsonWithCsrfAsync("/api/master/users", new
        {
            departmentId,
            loginId = "ito",
            password = "password",
            employeeName = "伊藤 次郎",
            roleCode = "USER"
        });
        var userJson = await api.ReadJsonAsync(userResponse);
        var userId = userJson.GetProperty("userId").GetInt32();

        Assert.Equal(HttpStatusCode.Created, userResponse.StatusCode);
        Assert.Equal("伊藤 次郎", userJson.GetProperty("employeeName").GetString());

        var updateUserResponse = await api.PutJsonWithCsrfAsync($"/api/master/users/{userId}", new
        {
            departmentId,
            loginId = "ito2",
            password = "",
            employeeName = "伊藤 三郎",
            roleCode = "ADMIN"
        });

        Assert.Equal(HttpStatusCode.OK, updateUserResponse.StatusCode);

        var stored = await fixture.QuerySingleAsync<StoredMasterUser>(
            """
            SELECT
                u.login_id AS LoginId,
                u.employee_name AS EmployeeName,
                u.role_code AS RoleCode,
                d.department_name AS DepartmentName
            FROM users u
            INNER JOIN departments d ON u.department_id = d.department_id
            WHERE u.user_id = @userId
            """,
            new { userId });
        Assert.Equal("ito2", stored.LoginId);
        Assert.Equal("伊藤 三郎", stored.EmployeeName);
        Assert.Equal("ADMIN", stored.RoleCode);
        Assert.Equal("運用改善部", stored.DepartmentName);
    }

    [SkippableFact]
    public async Task User_CannotReadMasterData()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync();

        var response = await api.HttpClient.GetAsync("/api/master/users");

        Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
    }

    [SkippableFact]
    public async Task CreateUser_ReturnsBadRequest_WhenInputIsInvalid()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.PostJsonWithCsrfAsync("/api/master/users", new
        {
            departmentId = 0,
            loginId = "",
            password = "short",
            employeeName = "",
            roleCode = "BAD"
        });
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Contains(json.GetProperty("errors").EnumerateArray(), e => e.GetString() == "部署は必須です。");
        Assert.Contains(json.GetProperty("errors").EnumerateArray(), e => e.GetString() == "権限の値が正しくありません。");
    }

    [SkippableFact]
    public async Task MasterData_ReturnsBadRequest_WhenUniqueConstraintsAreViolated()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var duplicateDepartment = await api.PostJsonWithCsrfAsync("/api/master/departments", new
        {
            departmentCode = "DEV",
            departmentName = "重複部署",
            displayOrder = 99
        });
        var duplicateDepartmentJson = await api.ReadJsonAsync(duplicateDepartment);

        Assert.Equal(HttpStatusCode.BadRequest, duplicateDepartment.StatusCode);
        Assert.Contains(
            duplicateDepartmentJson.GetProperty("errors").EnumerateArray(),
            e => e.GetString() == "部署コードは既に使用されています。");

        var duplicateUser = await api.PostJsonWithCsrfAsync("/api/master/users", new
        {
            departmentId = 1,
            loginId = "admin",
            password = "password",
            employeeName = "重複ユーザー",
            roleCode = "USER"
        });
        var duplicateUserJson = await api.ReadJsonAsync(duplicateUser);

        Assert.Equal(HttpStatusCode.BadRequest, duplicateUser.StatusCode);
        Assert.Contains(
            duplicateUserJson.GetProperty("errors").EnumerateArray(),
            e => e.GetString() == "ログインIDは既に使用されています。");
    }

    [SkippableFact]
    public async Task MasterData_ReturnsNotFound_WhenUpdatingMissingRecords()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var departmentResponse = await api.PutJsonWithCsrfAsync("/api/master/departments/99999", new
        {
            departmentCode = "NONE",
            departmentName = "存在しない部署",
            displayOrder = 99
        });

        Assert.Equal(HttpStatusCode.NotFound, departmentResponse.StatusCode);

        var userResponse = await api.PutJsonWithCsrfAsync("/api/master/users/99999", new
        {
            departmentId = 1,
            loginId = "none",
            password = "",
            employeeName = "存在しないユーザー",
            roleCode = "USER"
        });

        Assert.Equal(HttpStatusCode.NotFound, userResponse.StatusCode);
    }

    private sealed record StoredMasterUser(
        string LoginId,
        string EmployeeName,
        string RoleCode,
        string DepartmentName);
}
