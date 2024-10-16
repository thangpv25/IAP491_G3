# Agent Attacher và Taint Analysis
Attach agent vào các process đang chạy của JVM để thực hiện monitor. Sử dụng SootUp framework để chuyển bytecode thành dạng trung gian : Jimple, taint analysis dựa trên Control Flow Graph của các class và method được extract trong jimple.
