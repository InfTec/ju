package ch.inftec.ju.testing.db;


/**
 * EclipseLink SessionCustomizer. Used to override the PreallocationSize of the
 * sequence generator to 1 in order to be able to predict primary key IDs for
 * unit test cases, regardless of the order of the runs.
 * @author Martin
 *
 */
// FIXME: Migrate to hibernate
public class EclipseLinkSessionCustomizer {/*implements SessionCustomizer {

	@Override
	public void customize(Session s) throws Exception {
		s.getLogin().getDefaultSequence().setPreallocationSize(1);
	}
*/
}
