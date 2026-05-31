using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;
using WorkReport.Application.Mappers;

namespace WorkReport.Application.Auth;

public sealed class AuthService(IUserRepository userRepository, IPasswordHasher passwordHasher)
{
    public async Task<UserResponse?> AuthenticateAsync(LoginRequest request)
    {
        var user = await userRepository.FindByLoginIdAsync(request.LoginId);
        if (user is null || !passwordHasher.Verify(request.Password, user.PasswordHash))
        {
            return null;
        }

        return user.ToResponse();
    }
}
