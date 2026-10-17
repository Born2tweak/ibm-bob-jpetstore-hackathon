# JPetStore project scope

- The active hackathon application is jpetstore/. Original picclaus/ and cobolinfo/ material is reference work.
- Read SPEC.md, HACKATHON.md and VERIFICATION.md before changing behavior. Work on one agreed task with explicit acceptance checks.
- The existing fix protects sequential confirmation replay after successful insertion. Do not describe it as general idempotency or concurrent-request protection.
- The student track requires one useful feature. Recommend a bounded change first; do not silently expand into a new application, platform migration or unrelated feature.
- Preserve normal checkout, billing/shipping data, ownership checks and documented backorders. Do not reject negative inventory as a defect by assumption.
- Use JDK 21 for builds. From jpetstore/, the verified gate is: sh mvnw -B clean package failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT
- Keep direct-method/database evidence distinct from HTTP/browser and deployed evidence.
- Inspect the final diff and report the exact revision, commands, outcomes and remaining limits. Attribute only work actually performed in Bob.
- Preserve upstream licenses and notices. Keep generated target/ output and credentials out of commits.
