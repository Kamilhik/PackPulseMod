param(
    [string]$ProjectDir = "C:\Users\ksks1\Documents\FreeWeb Launcher\PackPulseMod",
    [string]$GradleExe = "C:\Users\ksks1\Documents\FreeWeb Launcher\gradle-8.8\bin\gradle.bat",
    [string]$Java17Home = "C:\Program Files\Java\jdk-17",
    [string]$Java21Home = "C:\Users\ksks1\Documents\FreeWeb Launcher\jdk-21\jdk-21.0.10+7"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $ProjectDir)) {
    throw "ProjectDir not found: $ProjectDir"
}
if (-not (Test-Path -LiteralPath $GradleExe)) {
    throw "Gradle executable not found: $GradleExe"
}
if (-not (Test-Path -LiteralPath $Java17Home)) {
    throw "Java 17 not found: $Java17Home"
}
if (-not (Test-Path -LiteralPath $Java21Home)) {
    throw "Java 21 not found: $Java21Home"
}

$releaseDir = Join-Path $ProjectDir "releases"
New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null

function Invoke-Build {
    param(
        [string]$MinecraftVersion,
        [string]$YarnMappings,
        [string]$MinecraftDependency,
        [string]$JavaHome,
        [string]$OutputName
    )

    Write-Host "Building for MC $MinecraftVersion with JAVA_HOME=$JavaHome"

    $env:JAVA_HOME = $JavaHome
    $env:Path = "$JavaHome\bin;$env:Path"

    & $GradleExe clean build `
        "-Pminecraft_version=$MinecraftVersion" `
        "-Pyarn_mappings=$YarnMappings" `
        "-Pminecraft_dependency=$MinecraftDependency" `
        --no-daemon `
        --stacktrace `
        --warning-mode=all `
        --project-dir "$ProjectDir"

    $sourceJar = Join-Path $ProjectDir "build\libs\packpulse-1.0.0.jar"
    if (-not (Test-Path -LiteralPath $sourceJar)) {
        throw "Build output not found: $sourceJar"
    }

    $targetJar = Join-Path $releaseDir $OutputName
    Copy-Item -LiteralPath $sourceJar -Destination $targetJar -Force
    Write-Host "Saved: $targetJar"
}

Invoke-Build `
    -MinecraftVersion "1.20.1" `
    -YarnMappings "1.20.1+build.10" `
    -MinecraftDependency "~1.20.1" `
    -JavaHome $Java17Home `
    -OutputName "packpulse-1.0.0-mc1.20.1.jar"

Invoke-Build `
    -MinecraftVersion "1.21.1" `
    -YarnMappings "1.21.1+build.3" `
    -MinecraftDependency "~1.21.1" `
    -JavaHome $Java21Home `
    -OutputName "packpulse-1.0.0-mc1.21.1.jar"

Write-Host ""
Write-Host "Done. Dual-version jars:"
Get-ChildItem -LiteralPath $releaseDir -Filter "*.jar" | Select-Object FullName, Length, LastWriteTime
