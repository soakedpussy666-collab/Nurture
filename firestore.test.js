const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const MIDWIFE_UID = "midwife_789";

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

test("Unauthenticated user: cannot read babies", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("babies").doc("baby1").get());
});

test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      role: "parent",
      displayName: "Alice Parent",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot impersonate or write another user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      role: "parent",
      displayName: "Bob Parent",
      email: "bob@example.com",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Parent: can create baby profile and log events", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("babies").doc("baby1").set({
      id: "baby1",
      name: "Leo",
      birthDate: "2026-09-15",
      caregiverUids: [ALICE_UID],
      midwifeUids: [MIDWIFE_UID],
      inviteCode: "BABY12",
      lmcInviteCode: "LMC789",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Can log a feeding event
  await assertSucceeds(
    aliceDb.collection("babies").doc("baby1").collection("logs").doc("log1").set({
      id: "log1",
      babyId: "baby1",
      type: "feeding",
      loggedByUid: ALICE_UID,
      loggedByName: "Alice",
      feedingType: "breast_left",
      durationMinutes: 15,
      memberUids: [ALICE_UID, MIDWIFE_UID],
      timestamp: new Date(),
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Non-member: cannot read baby logs", async () => {
  // Seed log
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("babies").doc("baby1").collection("logs").doc("log1").set({
      id: "log1",
      babyId: "baby1",
      type: "feeding",
      loggedByUid: ALICE_UID,
      loggedByName: "Alice",
      feedingType: "breast_left",
      durationMinutes: 15,
      memberUids: [ALICE_UID, MIDWIFE_UID],
      timestamp: new Date(),
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("babies").doc("baby1").collection("logs").doc("log1").get()
  );
});

test("Midwife: can read baby logs and send messages if member", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("babies").doc("baby1").set({
      id: "baby1",
      name: "Leo",
      birthDate: "2026-09-15",
      caregiverUids: [ALICE_UID],
      midwifeUids: [MIDWIFE_UID],
      inviteCode: "BABY12",
      lmcInviteCode: "LMC789",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
    await context.firestore().collection("babies").doc("baby1").collection("logs").doc("log1").set({
      id: "log1",
      babyId: "baby1",
      type: "feeding",
      loggedByUid: ALICE_UID,
      loggedByName: "Alice",
      feedingType: "bottle_milk",
      amountMl: 90,
      memberUids: [ALICE_UID, MIDWIFE_UID],
      timestamp: new Date(),
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const midwifeDb = testEnv.authenticatedContext(MIDWIFE_UID).firestore();
  await assertSucceeds(
    midwifeDb.collection("babies").doc("baby1").collection("logs").doc("log1").get()
  );

  // Midwife can send chat message
  await assertSucceeds(
    midwifeDb.collection("babies").doc("baby1").collection("messages").doc("msg1").set({
      id: "msg1",
      babyId: "baby1",
      senderUid: MIDWIFE_UID,
      senderName: "Midwife Sarah",
      senderRole: "midwife",
      text: "Great feeding volume today! How was his latch?",
      memberUids: [ALICE_UID, MIDWIFE_UID],
      createdAt: new Date(),
    })
  );
});
