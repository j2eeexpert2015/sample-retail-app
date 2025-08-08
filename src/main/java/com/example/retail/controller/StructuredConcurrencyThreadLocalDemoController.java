package com.example.retail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.StructuredTaskScope;

/*
 * Demonstrates ThreadLocal-based SecurityContext behavior with and without thread switches.
 *
 * SCENARIO A (baseline):
 *   WHAT: Entire request runs on a single Tomcat worker thread.
 *   WHY : SecurityContext is stored in a ThreadLocal bound to that thread → visible.
 *
 * SCENARIO B (structured-scope):
 *   WHAT: Parent request thread forks child VTHREADS via StructuredTaskScope.
 *   WHY : ThreadLocal does NOT propagate to new threads → children see empty SecurityContext.
 */
@RestController
public class StructuredConcurrencyThreadLocalDemoController {

    private static final Logger logger =
            LoggerFactory.getLogger(StructuredConcurrencyThreadLocalDemoController.class);

    /* SCENARIO A — Same-thread baseline
     * WHAT: Read SecurityContext on the request-handling thread.
     * WHY : No thread switch → ThreadLocal still bound → user is present.
     */
    @GetMapping("/security-demo/threadlocal/baseline")
    public BaselineResponse baselineSameThread() {
        // ThreadLocal lookup on the SAME thread that Spring Security populated after auth.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null ? authentication.getName() : "null");

        logger.info("baselineSameThread -> user={}, threadName={}, isVirtual={}",
                user, Thread.currentThread().getName(), Thread.currentThread().isVirtual());

        return new BaselineResponse(
                Thread.currentThread().getName(),
                user,
                "ThreadLocal is visible on the same request thread (no thread switch)."
        );
    }

    /* SCENARIO B — Structured concurrency (pre-fix)
     * WHAT: Fork two child VTHREADS using StructuredTaskScope and read SecurityContext in each.
     * WHY : SecurityContext is stored in a ThreadLocal on the PARENT thread; new child threads do NOT inherit it.
     *       Result: children see null Authentication unless you manually propagate or use a ScopedValue strategy.
     */
    @GetMapping("/security-demo/threadlocal/structured-scope")
    public StructuredScopeResponse structuredScopeNoPropagation() throws Exception {
        // For reference: confirm user is visible on the parent request thread.
        Authentication parentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        String parentUser = (parentAuthentication != null ? parentAuthentication.getName() : "null");
        String parentThread = Thread.currentThread().getName();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Each subtask runs in its OWN virtual thread.
            // WHY: ThreadLocal does NOT auto-propagate across threads → expect "null" below.
            var subtask1 = scope.fork(() -> {
                var t = Thread.currentThread();
                Authentication childAuth = SecurityContextHolder.getContext().getAuthentication();
                String childUser = (childAuth != null ? childAuth.getName() : "null");
                return "[threadName=" + t.getName() + ", isVirtual=" + t.isVirtual() + ", user=" + childUser + "]";
            });

            var subtask2 = scope.fork(() -> {
                var t = Thread.currentThread();
                Authentication childAuth = SecurityContextHolder.getContext().getAuthentication();
                String childUser = (childAuth != null ? childAuth.getName() : "null");
                return "[threadName=" + t.getName() + ", isVirtual=" + t.isVirtual() + ", user=" + childUser + "]";
            });

            scope.join(); // wait for both subtasks (no propagation fix applied)

            // Extract just the user names for the response, but keep the detailed strings in logs if needed
            String subtask1Summary = subtask1.get();
            String subtask2Summary = subtask2.get();

            logger.info("structuredScopeNoPropagation -> parentUser={}, parentThread={}, subtask1={}, subtask2={}",
                    parentUser, parentThread, subtask1Summary, subtask2Summary);

            // Keep the response focused on users; the note explains WHY it's null in children
            return new StructuredScopeResponse(
                    parentThread,
                    parentUser,
                    // parse the demo summaries just to show the user part crisply
                    subtask1Summary.contains("user=") ? subtask1Summary.split("user=")[1].replace("]", "") : "null",
                    subtask2Summary.contains("user=") ? subtask2Summary.split("user=")[1].replace("]", "") : "null",
                    "ThreadLocal SecurityContext is bound to the parent thread; child virtual threads do not inherit it by default."
            );
        }
    }

    // --------- DTOs ---------

    public record BaselineResponse(
            String threadName,  // request-handling thread name (Tomcat worker)
            String user,        // authenticated username or "null"
            String note         // short explanation (WHAT + WHY)
    ) { }

    public record StructuredScopeResponse(
            String parentThreadName, // parent request thread name (Tomcat worker)
            String parentUser,       // username on parent (expected: your user)
            String subtask1User,     // username in subtask 1 (expected: "null" pre-fix)
            String subtask2User,     // username in subtask 2 (expected: "null" pre-fix)
            String note              // WHY propagation fails here
    ) { }
}
