using WorkReport.Application.Contracts;
using WorkReport.Application.Masters;

namespace WorkReport.Tests.Application;

public sealed class MasterDataValidatorTests
{
    [Fact]
    public void DepartmentValidator_NormalizesCodeNameAndDisplayOrder()
    {
        var normalized = DepartmentValidator.Normalize(new DepartmentUpsertRequest(" ops ", " 運用部 ", null));

        Assert.Equal("OPS", normalized.DepartmentCode);
        Assert.Equal("運用部", normalized.DepartmentName);
        Assert.Equal(0, normalized.DisplayOrder);
    }

    [Fact]
    public void DepartmentValidator_ReturnsErrors_WhenInputIsInvalid()
    {
        var request = new DepartmentUpsertRequest("", "", -1);

        var errors = DepartmentValidator.Validate(request);

        Assert.Contains("部署コードは必須です。", errors);
        Assert.Contains("部署名は必須です。", errors);
        Assert.Contains("表示順は0以上で入力してください。", errors);
    }

    [Fact]
    public void UserValidator_NormalizesTextAndRoleCode()
    {
        var normalized = UserValidator.Normalize(new MasterUserCreateRequest(
            2,
            " sato ",
            "password",
            " 佐藤 花子 ",
            " user "));

        Assert.Equal("sato", normalized.LoginId);
        Assert.Equal("佐藤 花子", normalized.EmployeeName);
        Assert.Equal("USER", normalized.RoleCode);
    }

    [Fact]
    public void UserValidator_ReturnsPasswordError_WhenCreatePasswordIsMissing()
    {
        var request = new MasterUserCreateRequest(1, "sato", "", "佐藤 花子", "USER");

        var errors = UserValidator.Validate(request, requirePassword: true);

        Assert.Equal(["パスワードは必須です。"], errors);
    }

    [Fact]
    public void UserValidator_AllowsEmptyPassword_WhenUpdating()
    {
        var request = new MasterUserUpdateRequest(1, "sato", "", "佐藤 花子", "USER");

        var errors = UserValidator.Validate(request, requirePassword: false);

        Assert.Empty(errors);
    }

    [Fact]
    public void UserValidator_ReturnsErrors_WhenInputIsInvalid()
    {
        var request = new MasterUserCreateRequest(0, "", "short", "", "MANAGER");

        var errors = UserValidator.Validate(request, requirePassword: true);

        Assert.Contains("部署は必須です。", errors);
        Assert.Contains("ログインIDは必須です。", errors);
        Assert.Contains("パスワードは8文字以上で入力してください。", errors);
        Assert.Contains("社員名は必須です。", errors);
        Assert.Contains("権限の値が正しくありません。", errors);
    }
}
