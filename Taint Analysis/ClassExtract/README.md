Running:
1. mvn clean compile
2. mvn package
3. (Optional) Đổi **String agentPath = "target/class-extractor-agent-1.0-SNAPSHOT.jar";** nếu file jar được complie với tên khác hoặc ở path khác.
4. mvn exec:java -Dexec.mainClass="AgentAttacher"
