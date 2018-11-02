package net.praqma.jenkins.plugin.reloaded;

import hudson.matrix.*;
import hudson.model.Cause;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class FunctionalTests {

	@Rule
	public JenkinsRule r = new JenkinsRule();

	private AxisList axes = null;

	public MatrixProject initialize( String title ) throws IOException {
		axes = new AxisList( new Axis( "dim1", "1", "2" ), new Axis( "dim2", "a", "b" ) );
		MatrixProject mp = r.createProject(MatrixProject.class, "matrix-reloaded-test-" + title );
		mp.setAxes( axes );
		return mp;
	}

    @Test
	public void test() throws IOException, InterruptedException, ExecutionException {
		MatrixProject mp = initialize( "basic" );

		/* Run first build */
		MatrixBuild mb = mp.scheduleBuild2( 0, new Cause.UserCause() ).get();

		/* Reload it */
		/* Create form elements */
		Map<String, String[]> form = new HashMap<String, String[]>();

		/* Base build is 1 */
		form.put( "MRP::NUMBER", new String[] { "1" } );
		form.put( "MRP::dim1=1,dim2=b", new String[] { "0" } );
		form.put( "MRP::dim1=2,dim2=a", new String[] { "0" } );

		MatrixReloadedAction mra = new MatrixReloadedAction();
		RebuildAction raction = mra.getRebuildAction( form );

		MatrixBuild mb2 = mp.scheduleBuild2( 0, new Cause.UserCause(), raction ).get();

		RebuildAction raction2 = (RebuildAction) mb2.getAction( RebuildAction.class );

		/* Test base builds */
		assertEquals( 1, raction2.getBaseBuildNumber() );
		assertEquals( mb, mb2.getBaseBuild() );

		/* Not rebuild */
		Map<String, String> r1 = new HashMap<String, String>();
		r1.put( "dim1", "1" );
		r1.put( "dim2", "a" );
		Combination combination1a = new Combination( r1 );
		
		/* Rebuild */
		Map<String, String> r2 = new HashMap<String, String>();
		r2.put( "dim1", "1" );
		r2.put( "dim2", "b" );
		Combination combination1b = new Combination( r2 );

		assertEquals( 1, mb2.getRun( combination1a ).getNumber() );
		assertEquals( 2, mb2.getRun( combination1b ).getNumber() );
		
		System.out.println( "1A: " + mb2.getRun( combination1a ).getNumber() );
		System.out.println( "1B: " + mb2.getRun( combination1b ).getNumber() );
	}

}
