package org.jenkinsci.plugins.scm_filter;

import java.io.IOException;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugin.gitea.GiteaSCMSource;
import org.jenkinsci.plugin.gitea.PullRequestSCMRevision;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitDetail;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;

public final class GiteaUtils {

	private GiteaUtils() {
		// utils class, no instances
	}

	public static GiteaCommitDetail getCommit(SCMSource source, SCMRevision revision) throws CouldNotGetCommitDataException {
		String hash = null;
		if (AbstractGitSCMSource.SCMRevisionImpl.class.isAssignableFrom(revision.getClass())){
			hash = ((AbstractGitSCMSource.SCMRevisionImpl) revision).getHash();
		}
		if (PullRequestSCMRevision.class.isAssignableFrom(revision.getClass())){
			hash = ((PullRequestSCMRevision) revision).getOrigin().getHash();
		}
		if (hash == null) {
			throw new CouldNotGetCommitDataException("Unknown revision class ["+revision.getClass()+"] or null hash");
		}

		if (!GiteaSCMSource.class.isAssignableFrom(source.getClass())){
			throw new IllegalArgumentException("SCM Source ["+source.getClass()+"] is not a GiteaSCMSource ");
		}
		GiteaSCMSource giteaSource = (GiteaSCMSource) source;

		try (GiteaConnection gitea = Gitea.server(giteaSource.getServerUrl()).as(AuthenticationTokens.convert(GiteaAuth.class, giteaSource.credentials())).open()) {
			return gitea.fetchCommit(giteaSource.getRepoOwner(), giteaSource.getRepository(), hash);
		} catch (IOException | InterruptedException e) {
			throw new CouldNotGetCommitDataException(e);
		}
	}
}
