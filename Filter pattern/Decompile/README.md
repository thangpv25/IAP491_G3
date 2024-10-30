# Agent Decompile Java

## Tổng Quan

- Code này thực hiện decompile một tiến trình Java đang chạy trong JVM thành mã nguồn.

## Hướng Dẫn

1. **Build Agent**
   - Đây là một agent. Để sử dụng, cần build thành tệp JAR bằng Maven:
     ```bash
     mvn clean package
     ```

2. **Attach Agent**
   - Sau khi JAR được build, có thể attach nó vào tiến trình Java mong muốn.


