package org.jenkinsci.plugins.scm_filter;

import io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class GitLabCommitSkipTraitTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    @Rule
    public TestName currentTestName = new TestName();

    private SCMSource load() {
        return load(currentTestName.getMethodName());
    }

    private SCMSource load(String dataSet) {
        return (GitLabSCMSource) Jenkins.XSTREAM2.fromXML(
                getClass().getResource(getClass().getSimpleName() + "/" + dataSet + ".xml"));
    }

    @Test
    public void skipci() throws Exception {
        GitLabSCMSource instance = (GitLabSCMSource) load();
        assertThat(instance.getTraits(), contains(instanceOf(GitLabCommitSkipTrait.class)));
    }
}
