FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

COPY backend/WorkReport.Api/WorkReport.Api.csproj backend/WorkReport.Api/
RUN dotnet restore backend/WorkReport.Api/WorkReport.Api.csproj

COPY backend/WorkReport.Api/ backend/WorkReport.Api/
RUN dotnet publish backend/WorkReport.Api/WorkReport.Api.csproj -c Release -o /app/publish --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app

ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080

COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "WorkReport.Api.dll"]
