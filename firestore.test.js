const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "gen-lang-client-0646913099";
const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

let testEnv;

function pastDate() {
  return new Date(Date.now() - 1000);
}

function futureDate() {
  return new Date(Date.now() + 60 * 60 * 1000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

function dbFor(uid, tokenOptions = undefined) {
  const ctx = uid
    ? testEnv.authenticatedContext(uid, tokenOptions)
    : testEnv.unauthenticatedContext();
  return ctx.firestore();
}

function validProfile(uid) {
  const now = pastDate();
  return {
    userId: uid,
    displayName: "Ahmed Khan",
    email: "ahmed@example.com",
    phone: "+92 300 1234567",
    roleView: "student",
    hadithAr: "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
    hadithTr: "Seeking knowledge is an obligation upon every Muslim.",
    hadithSrc: "Sunan Ibn Majah",
    bannerCaption: "",
    seeded: true,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: denied all reads and writes", async () => {
  const unauthDb = dbFor(null);
  await assertFails(unauthDb.doc("users/alice").get());
  await assertFails(unauthDb.doc("users/alice").set(validProfile("alice")));
  await assertFails(unauthDb.collection("users/alice/courses").get());
  await assertFails(unauthDb.collection("users/alice/ai_chats").get());
});

test("UserProfile: owner create/read/partial update succeed; shadow fields, future timestamps, and cross-user access fail", async () => {
  const aliceDb = dbFor("alice");
  const bobDb = dbFor("bob");
  const adminDb = dbFor("admin_uid", {
    email: "alfalahislamiconlineinstitute1@gmail.com",
    email_verified: true,
  });

  await assertSucceeds(aliceDb.doc("users/alice").set(validProfile("alice")));
  await assertSucceeds(aliceDb.doc("users/alice").get());

  // Partial update matching Kotlin repository
  await assertSucceeds(
    aliceDb.doc("users/alice").update({
      displayName: "Ahmed Updated",
      roleView: "admin",
      hadithTr: "Updated Hadith translation.",
      updatedAt: pastDate(),
    })
  );

  // Rejects shadow field
  await assertFails(
    aliceDb.doc("users/alice").update({
      isAdmin: true,
      updatedAt: pastDate(),
    })
  );

  // Rejects future timestamp
  await assertFails(
    aliceDb.doc("users/alice").update({
      displayName: "Future",
      updatedAt: futureDate(),
    })
  );

  // Rejects cross-user read and update
  await assertFails(bobDb.doc("users/alice").get());
  await assertFails(
    bobDb.doc("users/alice").update({
      displayName: "Hijacked",
      updatedAt: pastDate(),
    })
  );

  // Bootstrapped admin can read
  await assertSucceeds(adminDb.doc("users/alice").get());
});

test("Courses: CRUD, bounds, @DocumentId id rejection, orderBy query, and cross-user isolation", async () => {
  const aliceDb = dbFor("alice");
  const bobDb = dbFor("bob");
  const now = pastDate();

  const validCourse = {
    userId: "alice",
    title: "Noorani Qaida",
    teacher: "Qari Bilal Ahmed",
    desc: "Foundational course for Arabic letters.",
    duration: "3 months",
    timing: "Mon-Fri, 5:00 PM",
    students: 42,
    progress: 65,
    icon: "book",
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(aliceDb.doc("users/alice/courses/c1").set(validCourse));
  await assertSucceeds(
    aliceDb.collection("users/alice/courses").orderBy("createdAt", "asc").get()
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/courses/c1").update({
      students: 43,
      progress: 70,
      updatedAt: pastDate(),
    })
  );

  // Rejects @DocumentId 'id' serialized into payload
  await assertFails(
    aliceDb.doc("users/alice/courses/c2").set({ ...validCourse, id: "c2" })
  );

  // Rejects out-of-range progress
  await assertFails(
    aliceDb.doc("users/alice/courses/c1").update({
      progress: 150,
      updatedAt: pastDate(),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users/alice/courses").get());
  await assertFails(bobDb.doc("users/alice/courses/c1").delete());

  await assertSucceeds(aliceDb.doc("users/alice/courses/c1").delete());
});

test("Subcollections: classes, videos, materials, assignments, students, teachers, updates, notifications, ai_chats", async () => {
  const aliceDb = dbFor("alice");
  const bobDb = dbFor("bob");
  const now = pastDate();

  // OnlineClass
  await assertSucceeds(
    aliceDb.doc("users/alice/classes/cls1").set({
      userId: "alice",
      course: "Hifz-ul-Quran",
      teacher: "Hafiz Usman Tariq",
      date: "Today",
      time: "4:00 PM",
      status: "live",
      link: "https://meet.google.com/demo",
      createdAt: now,
      updatedAt: now,
    })
  );

  // VideoLesson
  await assertSucceeds(
    aliceDb.doc("users/alice/videos/v1").set({
      userId: "alice",
      title: "Makharij al-Huroof Explained",
      teacher: "Qari Bilal Ahmed",
      desc: "Correct articulation points.",
      duration: "12:40",
      url: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // StudyMaterial
  await assertSucceeds(
    aliceDb.doc("users/alice/materials/m1").set({
      userId: "alice",
      title: "Tajweed Notes",
      course: "Quran Tajweed",
      type: "Notes",
      uploaderName: "Ahmed",
      uploaderRole: "student",
      uploaderEmail: "ahmed@example.com",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Assignment create + submit + feedback + invalid status rejection
  await assertSucceeds(
    aliceDb.doc("users/alice/assignments/a1").set({
      userId: "alice",
      title: "Tajweed Rule Worksheet 3",
      course: "Quran Tajweed",
      due: "Sep 22",
      status: "pending",
      description: "Complete exercises.",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/assignments/a1").update({
      status: "submitted",
      updatedAt: pastDate(),
    })
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/assignments/a1").update({
      status: "reviewed",
      feedback: "MashAllah, excellent work!",
      updatedAt: pastDate(),
    })
  );
  await assertFails(
    aliceDb.doc("users/alice/assignments/a1").update({
      status: "invalid_status",
      updatedAt: pastDate(),
    })
  );

  // StudentRecord & TeacherRecord
  await assertSucceeds(
    aliceDb.doc("users/alice/students/s1").set({
      userId: "alice",
      name: "Fatima Noor",
      email: "fatima@example.com",
      city: "Lahore",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/students/s1").update({
      status: "approved",
      updatedAt: pastDate(),
    })
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/teachers/t1").set({
      userId: "alice",
      name: "Qari Bilal Ahmed",
      subject: "Tajweed",
      status: "active",
      createdAt: now,
      updatedAt: now,
    })
  );

  // IslamicUpdate
  await assertSucceeds(
    aliceDb.doc("users/alice/islamic_updates/u1").set({
      userId: "alice",
      title: "Ramadan Wallpaper",
      category: "Wallpaper",
      link: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Notification (immutable after create)
  await assertSucceeds(
    aliceDb.doc("users/alice/notifications/n1").set({
      userId: "alice",
      title: "Announcement",
      body: "New class scheduled.",
      createdAt: now,
    })
  );
  await assertFails(
    aliceDb.doc("users/alice/notifications/n1").update({ title: "Edited" })
  );

  // AI Chats (create + orderBy + delete allowed; update denied; cross-user denied)
  await assertSucceeds(
    aliceDb.doc("users/alice/ai_chats/msg1").set({
      userId: "alice",
      sender: "user",
      text: "Explain the rules of Noon Sakinah.",
      topic: "Tajweed",
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.doc("users/alice/ai_chats/msg2").set({
      userId: "alice",
      sender: "assistant",
      text: "There are four rules: Izhar, Idgham, Iqlab, and Ikhfa.",
      topic: "Tajweed",
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users/alice/ai_chats").orderBy("createdAt", "asc").get()
  );
  await assertFails(
    aliceDb.doc("users/alice/ai_chats/msg1").update({ text: "Tampered" })
  );
  await assertFails(bobDb.collection("users/alice/ai_chats").get());
  await assertSucceeds(aliceDb.doc("users/alice/ai_chats/msg1").delete());
});

test("Admins collection: prevents client writes and non-admin listing", async () => {
  const aliceDb = dbFor("alice");
  await assertFails(aliceDb.doc("admins/alice").set({ active: true }));
  await assertFails(aliceDb.collection("admins").get());
});
