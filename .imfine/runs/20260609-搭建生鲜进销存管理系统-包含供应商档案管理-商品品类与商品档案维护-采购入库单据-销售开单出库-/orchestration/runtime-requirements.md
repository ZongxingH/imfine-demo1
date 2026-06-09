# Runtime Requirements

- status: blocked
- project kind: new_project
- declared runtimes: missing
- declaration files: missing

## Observed Versions

- missing

## QA Evidence

- file: .imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/test-results.md
- records runtime version: no
- records test command: yes
- records test output: yes

## Checks

- pass: project_docs (new project delivery requires README or runbook)
- blocked: runtime_version_declaration (no language manifest detected; runtime declaration still required before archive)
- blocked: runtime_version_observed (no declared or detected runtime to observe)
- blocked: qa_records_runtime_version (QA evidence must include actual runtime version output)
- pass: qa_records_test_command (QA evidence must include executed test command)
- pass: qa_records_test_output (QA evidence must include actual test output, not just a summary word)
