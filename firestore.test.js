const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student";
const BOB_UID = "bob_teacher";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot create a user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: "alice@school.edu",
      role: "student",
      isVerified: false,
    })
  );
});

test("authenticated user can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: "alice@school.edu",
      role: "student",
      isVerified: false,
    })
  );
});

test("user cannot create or overwrite another user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      id: BOB_UID,
      name: "Bob",
      email: "bob@school.edu",
      role: "teacher",
      isVerified: false,
    })
  );
});

test("teacher can create homework", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("homework").doc("hw1").set({
      id: "hw1",
      title: "Algebra Chapter 3 Exercises",
      subject: "Mathematics",
      teacherId: BOB_UID,
      teacherName: "Mr. Bob",
    })
  );
});

test("unauthenticated user cannot create homework", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("homework").doc("hw1").set({
      id: "hw1",
      title: "Algebra Chapter 3 Exercises",
      subject: "Mathematics",
      teacherId: BOB_UID,
      teacherName: "Mr. Bob",
    })
  );
});
