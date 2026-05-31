FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

COPY backend/WorkReport.Domain/WorkReport.Domain.csproj backend/WorkReport.Domain/
COPY backend/WorkReport.Application/WorkReport.Application.csproj backend/WorkReport.Application/
COPY backend/WorkReport.Infrastructure/WorkReport.Infrastructure.csproj backend/WorkReport.Infrastructure/
COPY backend/WorkReport.Api/WorkReport.Api.csproj backend/WorkReport.Api/
RUN dotnet restore backend/WorkReport.Api/WorkReport.Api.csproj

COPY backend/WorkReport.Domain/ backend/WorkReport.Domain/
COPY backend/WorkReport.Application/ backend/WorkReport.Application/
COPY backend/WorkReport.Infrastructure/ backend/WorkReport.Infrastructure/
COPY backend/WorkReport.Api/ backend/WorkReport.Api/
RUN dotnet publish backend/WorkReport.Api/WorkReport.Api.csproj -c Release -o /app/publish --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app

ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080

COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "WorkReport.Api.dll"]
