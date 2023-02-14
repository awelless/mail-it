package io.mailit.distribution.graalvm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import freemarker.log._Log4jOverSLF4JTester;

/**
 * To avoid a class not found error on org.apache.log4j.MDC
 */
@TargetClass(_Log4jOverSLF4JTester.class)
final class _Log4jOverSLF4JTesterSubstitute {

    @Substitute
    public static final boolean test() {
        return false;
    }
}
