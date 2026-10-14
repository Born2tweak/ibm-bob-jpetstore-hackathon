# IBM Bob hackathon project record

## Current direction

- **Submission application:** JPetStore in `jpetstore/`.
- **Upstream:** https://github.com/mybatis/jpetstore-6 at `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf` (`master`). Its tracked files were copied without Git metadata or build output. Keep its Apache 2.0 `LICENSE`, `LICENSE_HEADER`, and `NOTICE` with the code.
- **Team repository:** https://github.com/Born2tweak/ibmi-cobol-hackathon. The existing IBM i COBOL examples and their history remain intact as prior exploration.
- **Preparation:** Codex performed the baseline import, documentation, and local verification. None of that is IBM Bob work.
- **IBM Bob work:** Not started or evidenced here. Record later prompts, accepted changes, human review, commits, and verification separately.
- **Deadline:** October 18, 2026, 11:59 PM EDT.

## Next decisions

Reproduce one candidate issue from [SPEC.md](SPEC.md) in the running app, agree on expected behavior, then use Bob for a bounded fix and useful feature. Keep the issue and feature provisional until their behavior and value are established. Preserve the imported baseline for comparison. Do not treat backorders or negative inventory as a defect without stronger evidence: `jpetstore/src/main/webapp/WEB-INF/jsp/catalog/Item.jsp` labels nonpositive stock “Back ordered,” and upstream `help.html` describes backordering.

[VERIFICATION.md](VERIFICATION.md) records executable commands and current results. A live judge URL, final fix, feature, demo, and submission materials remain pending.
