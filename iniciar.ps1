# Inicia Django, React y Spring Boot en ventanas separadas. MySQL se enciende desde XAMPP.
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not (Test-NetConnection -ComputerName 127.0.0.1 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue)) {
    Write-Host "MySQL no responde en el puerto 3306. Enciéndelo desde XAMPP y vuelve a ejecutar este script." -ForegroundColor Red
    exit 1
}

Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle='Django'; cd '$raiz\admin-django'; .\venv312\Scripts\Activate.ps1; python manage.py runserver"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle='React'; cd '$raiz\admin-react'; npm run dev"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle='Spring Boot'; `$env:MEDIA_DIR='$raiz\admin-django\media'; cd '$raiz\web-springboot\agricultor\agricultor'; .\mvnw.cmd spring-boot:run"

Write-Host "Servidores iniciados. Panel: http://localhost:5173  |  Django: http://127.0.0.1:8000/admin/  |  API: http://127.0.0.1:8080/api/parcelas"
