param(
    [string]$SourcePath
)

if (-not (Test-Path $SourcePath)) {
    Write-Host "Dossier introuvable : $SourcePath"
    exit
}

# ============================================================
# VERIFIER EXIFTOOL
# ============================================================

$exiftool = Get-Command exiftool -ErrorAction SilentlyContinue

if ($null -eq $exiftool) {

    Write-Host ""
    Write-Host "ExifTool n'est pas installé."
    Write-Host ""
    Write-Host "Télécharge : https://exiftool.org/"
    Write-Host ""
    exit
}

# ============================================================
# TRAITEMENT
# ============================================================

Get-ChildItem $SourcePath -File | ForEach-Object {

    $file = $_

    try {

        # ====================================================
        # Lire plusieurs dates possibles
        # ====================================================

        $dateString = exiftool `
            -s3 `
            -DateTimeOriginal `
            -CreateDate `
            -MediaCreateDate `
            -TrackCreateDate `
            -FileModifyDate `
            $file.FullName `
            | Select-Object -First 1

        if ([string]::IsNullOrWhiteSpace($dateString)) {

            Write-Host "Pas de date : $($file.Name)"
            return
        }

        # ====================================================
        # Nettoyage format ExifTool
        # Ex:
        # 2024:10:27 15:14:33
        # ====================================================

        $clean =
            $dateString.Split(" ")[0].Replace(":", "-")

        $date =
            [datetime]::Parse($clean)

        $targetFolder =
            Join-Path `
                $SourcePath `
                ($date.ToString("yyyy-MM"))

        if (!(Test-Path $targetFolder)) {

            New-Item `
                -ItemType Directory `
                -Path $targetFolder | Out-Null
        }

        Move-Item `
            -Path $file.FullName `
            -Destination $targetFolder

        Write-Host `
            "$($file.Name) -> $($date.ToString('yyyy-MM'))"

    }
    catch {

        Write-Host `
            "Erreur : $($file.Name)"
    }
}