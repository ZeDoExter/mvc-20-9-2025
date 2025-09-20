Overview
- ใช้ Spring Boot + Thymeleaf เก็บข้อมูลเป็นไฟล์ JSON
- ข้อมูลจะถูก seed ให้อัตโนมัติครับ ถ้ากดรันแล้ว

โครงสร้าง MVC และไฟล์สำคัญ (ทำงานร่วมกันยังไง)

Model (ข้อมูลโดเมน + Model สำหรับคำนวณ)

- โดเมนหลัก
  - `src/main/java/com/ZeDoExter/mvc/model/Project.java` — โครงการ: id, name, category, goalAmount, deadline, createdAt, currentRaised, hasStretchGoals, rejectedPledges, rewardTiers, stretchGoals
  - `src/main/java/com/ZeDoExter/mvc/model/RewardTier.java` — ระดับรางวัล: name, minAmount, quota รวม/คงเหลือ
  - `src/main/java/com/ZeDoExter/mvc/model/Pledge.java` — การสนับสนุน: userId, projectId, amount, rewardTierId (ถ้ามี), timestamp, status, rejectionReason
  - `src/main/java/com/ZeDoExter/mvc/model/StretchGoal.java` — เป้าหมายเพิ่มเติม: thresholdAmount และคำอธิบาย
  - `src/main/java/com/ZeDoExter/mvc/model/User.java` — ผู้ใช้แบบง่าย: username/password/displayName
  - `src/main/java/com/ZeDoExter/mvc/model/PledgeStatus.java` — สถานะ ACCEPTED/REJECTED

- Model (business logic)
  - `src/main/java/com/ZeDoExter/mvc/model/BasicFundingModel.java` — ใช้ตอบว่า "ถึงเป้าหมายหลักหรือยัง"
  - `src/main/java/com/ZeDoExter/mvc/model/StretchFundingModel.java` — ใช้บอกว่าเป้าหมายย่อย (stretch) ปลดล็อกถึงระดับไหนแล้ว

Repository / Datastore
- `src/main/java/com/ZeDoExter/mvc/repository/DataStore.java` — ตัวกลาง IO กับไฟล์ JSON (users.json, projects.json, pledges.json) และคำนวณค่าต่างๆ (ยอดรวม, โควตาคงเหลือ, จำนวน reject ต่อโปรเจกต์)
- `src/main/java/com/ZeDoExter/mvc/repository/ProjectRepository.java` — ดึงโปรเจกต์ เรียงตาม deadline หรือหาเป็นรายตัว
- `src/main/java/com/ZeDoExter/mvc/repository/PledgeRepository.java` — บันทึก/ดึงรายการ Pledge
- `src/main/java/com/ZeDoExter/mvc/repository/UserRepository.java` — หาผู้ใช้ตาม username/id

Services
- `src/main/java/com/ZeDoExter/mvc/service/ProjectService.java` — รวมการดึงรายการโปรเจกต์ + ฟิลเตอร์/เรียง (category, sort = deadline, newest, top) และแปลงเป็น Basic/Stretch models
- `src/main/java/com/ZeDoExter/mvc/service/PledgeService.java` — การรับ/ปฏิเสธการสนับสนุน ตาม Controller
- `src/main/java/com/ZeDoExter/mvc/service/AuthService.java` — Login แบบง่าย (เทียบ username/password ตรงๆ)
- `src/main/java/com/ZeDoExter/mvc/service/StatsService.java` — นับจำนวน Pledge ที่ ACCEPTED/REJECTED เพื่อทำหน้าสถิติ

Controllers
- `src/main/java/com/ZeDoExter/mvc/controller/ProjectController.java`
  - GET `/projects` — หน้ารวมโครงการ (รองรับ `?category=` และ `?sort = deadline, newest, top`)
  - GET `/projects/{id}` — หน้ารายละเอียดโครงการ + แสดง progress + stretch goals + ฟอร์ม pledge
  - POST `/projects/{id}/pledge` — ส่งฟอร์มสนับสนุน (ต้อง login)
- `src/main/java/com/ZeDoExter/mvc/controller/LoginController.java`
  - GET `/login` — แบบฟอร์ม Login (รองรับ `?redirect=`)
  - POST `/login` — ตรวจ username/password แล้วเก็บ session
  - GET `/logout` — ออกจากระบบ (ลบ session)
- `src/main/java/com/ZeDoExter/mvc/controller/StatsController.java`
  - GET `/stats` — หน้าสรุปสถิติ จำนวนสำเร็จ/ถูกปฏิเสธทั้งหมด
- `src/main/java/com/ZeDoExter/mvc/controller/AdminController.java`
  - GET `/admin/seed/reset` — รีเซ็ตไฟล์ข้อมูลและ seed ชุดตัวอย่างใหม่ที่หลากหลาย

View (Thymeleaf Templates + Static Assets)
- Fragment
  - `src/main/resources/templates/fragments/navbar.html` — Navbar กลาง (ดึงด้วย `th:replace`) และรับตัวแปร `active` จาก Controller เพื่อเน้นเมนูปัจจุบัน
- หน้า
  - `src/main/resources/templates/projects/list.html` — หน้ารวมโครงการ (ฟิลเตอร์หมวดหมู่ + เรียงลำดับ + autosubmit เมื่อเปลี่ยน)
  - `src/main/resources/templates/projects/detail.html` — หน้ารายละเอียด (progress bar, stretch goals, แบบฟอร์ม pledge พร้อม quota/ขั้นต่ำ)
  - `src/main/resources/templates/stats.html` — หน้าสรุปสถิติที่ดึงจาก StatsService
  - `src/main/resources/templates/login.html` — แบบฟอร์ม Login

Routes/Actions + หน้าจอสำคัญ
- GET `/projects`
  - แสดงรายการโครงการ (เรียงตามตัวเลือก) พร้อม progress bar, หมวดหมู่, deadline
  - ฟิลเตอร์: `?category=Tech` (หรือหมวดหมู่อื่น)
  - เรียงลำดับ: `?sort=deadline|newest|top`
  - เมื่อเปลี่ยน selector จะ autosubmit ให้ทันที
- GET `/projects/{id}`
  - รายละเอียดโครงการ: เป้าหมาย/ยอดระดม, progress, stretch goals (ปลดล็อกหรือยัง), quota ของรางวัล
  - แบบฟอร์มส่ง Pledge (ต้อง login)
- POST `/projects/{id}/pledge`
  - ระบบตรวจตามกติกา ถ้าผ่านจะตัด quota และเพิ่มยอดรวมทันที
  - ถ้าไม่ผ่าน จะแสดงข้อความปฏิเสธและบันทึกเป็น REJECTED
- GET `/login`, POST `/login`, GET `/logout`
  - Login แบบง่าย เก็บชื่อผู้ใช้ใน session เพื่อแสดงบน navbar และใช้ตอนบันทึก pledge
- GET `/stats`
  - แสดงจำนวน Accepted / Rejected รวมทั้งระบบ (จากไฟล์ JSON)
- GET `/admin/seed/reset`
  - ปุ่มลัดสำหรับรีเซ็ตข้อมูลชุดตัวอย่าง (ช่วยทดสอบสถานการณ์ต่างๆ ได้เร็ว)

ข้อมูล/ฐานข้อมูล (JSON)
- โฟลเดอร์ `data/` อยู่ใน root ด้านนอกสุด (ถูกสร้างเมื่อรันครั้งแรก)
  - `data/users.json`
  - `data/projects.json`
  - `data/pledges.json`

ชุดข้อมูลตัวอย่าง
- มีโปรเจกต์หลายหมวดหมู่, ทั้งแบบมี/ไม่มี Stretch Goal
- มีโปรเจกต์ที่หมดเวลาแล้ว
- มีรางวัลที่เหลือโควตา 1 เพื่อดู state ใกล้หมด และมีรางวัลหมดโควตาแล้ว
- มีทั้ง Accepted/Rejected จากหลายสาเหตุ (ต่ำกว่าขั้นต่ำ, จำนวนเงินไม่ถูกต้อง, โควตาหมด, หมดเวลา)
- มี createdAt ต่างกัน เพื่อให้ sort แบบใหม่สุดทำงานเห็นผล

flow การทำงานสั้นๆ
1) ผู้ใช้เปิด /projects → Controller ดึงจาก Service → Service เรียก Repository → Repository ใช้ DataStore → DataStore อ่าน JSON และคำนวณ progress/โควตา → ส่งข้อมูลให้ View แสดง
2) ผู้ใช้เปิด /projects/{id} → ดูรายละเอียด + ฟอร์ม pledge
3) ผู้ใช้ (ที่ login แล้ว) ส่งฟอร์ม → POST /projects/{id}/pledge → PledgeService ตรวจตามกติกา → บันทึกผลลง JSON ผ่าน Repository/DataStore → คำนวณค่าอนุพันธ์ใหม่ แล้ว redirect กลับหน้าเดิมพร้อมข้อความสำเร็จ/ปฏิเสธ
4) /stats อ่านสรุปจาก StatsService (นับในไฟล์ JSON) แสดงการ์ด Accepted/Rejected
