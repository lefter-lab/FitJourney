Виждам точно проблема. Maven Runner-ът в IntelliJ е на Java 17, но терминалът (PowerShell) използва системния Java = JDK 25.
Забележи в лога:
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by lombok.permit.Permit
Това е симптом на JDK 25, не 17.

Провери коя Java се вика от PowerShell:
powershelljava -version
echo $env:JAVA_HOME
Най-вероятно ще видиш 25.0.2.

Решението — стартирай Maven от IntelliJ, не от терминала:
Вместо да пишеш mvn clean compile в PowerShell, използвай:

Maven панела в IntelliJ (вдясно M иконата) → Lifecycle → compile
Или Ctrl+F9 (Build Project)

Така Maven ще използва JDK 17 от IntelliJ настройките, а не системния JDK 25.

Ако искаш да го оправиш и в терминала — добави в PowerShell профила си:
powershell$env:JAVA_HOME = "C:\Users\admin\.jdks\ms-17.0.18"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
(пътят се вижда директно от скрийншота ти: C:/Users/admin/.jdks/ms-17.0.18)You said: PS D:\изтегляния download\Spring Fundamentals - май 2026\Wallet\02-Smart-Wallet-Application-Resources\smart-wallet-application> java -versionPS D:\изтегляния download\Spring Fundamentals - май 2026\Wallet\02-Smart-Wallet-Application-Resources\smart-wallet-application> java -version
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)
PS D:\изтегляния download\Spring Fundamentals - май 2026\Wallet\02-Smart-Wallet-Application-Resources\smart-wallet-application> echo $env:JAVA_HOME15:42Claude responded: Точно както очаквах — системният Java е 25, и JAVA_HOME е празен.Точно както очаквах — системният Java е 25, и JAVA_HOME е празен.
Изпълни това в същия PowerShell прозорец и след това пробвай mvn clean compile:
powershell$env:JAVA_HOME = "C:\Users\admin\.jdks\ms-17.0.18"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
След това провери:
powershelljava -version
Трябва да покаже 17.0.18. Тогава mvn clean compile ще работи.

Важно: това е временна промяна само за текущия терминален прозорец. Ако затвориш и отвориш нов — пак ще е Java 25. За постоянна промяна трябва да се смени от Windows Environment Variables, но за сега това е достатъчно.