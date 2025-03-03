import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class Testing {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    // TODO: Write your tests here!

    @Test
    @DisplayName("Test synchronize() - front case")
    public void testSynchronizeFront() throws InterruptedException {
        String[] repo1Messages = {"r1 c1", "r1 c2", "r1 c3"};
        commitAll(repo1, repo1Messages);
        String[] repo2Messages = {"r2 c1", "r2 c2", "r2 c3"};
        commitAll(repo2, repo2Messages);

        repo1.synchronize(repo2);
        String[] correctOrdering = {"r1 c1", "r1 c2", "r1 c3", "r2 c1", "r2 c2", "r2 c3"};

        assertEquals(6, repo1.getRepoSize()); // checking if the repo size is correct
        assertEquals(null, repo2.getRepoHead()); // check if repo2 gets nullified after calling synchronize
        testHistory(repo1, 6, correctOrdering);
    }

    @Test
    @DisplayName("Test synchronize() - middle case")
    public void testSynchronizeMiddle() throws InterruptedException {
        String[] repo1Messages = {"OLDEST", "SUPER OLD"};
        commitAll(repo1, repo1Messages);
        String[] repo2Messages = {"SLIGHTLY LESS OLD", "BARELY OLD"};
        commitAll(repo2, repo2Messages);

        String[] newRepo1Messages = {"PLAIN OLD", "BARELY NEW"};
        commitAll(repo1, newRepo1Messages);
        String[] newRepo2Messages = {"SLIGHTLY NEWER", "SHINY BRAND NEW"};
        commitAll(repo2, newRepo2Messages);

        repo1.synchronize(repo2);
        assertEquals(8, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());

        String[] correctOrdering = {"OLDEST", "SUPER OLD", "SLIGHTLY LESS OLD", "BARELY OLD", 
                                   "PLAIN OLD", "BARELY NEW", "SLIGHTLY NEWER", "SHINY BRAND NEW"};
        testHistory(repo1, 8, correctOrdering);
    }

    @Test
    @DisplayName("Test synchronize() - empty case (part 1)")
    public void testSynchronizeEmpty1() throws InterruptedException {
        // check if repo1 has things and repo2 is empty
        String[] repo1Messages = {"1", "2", "3"};
        commitAll(repo1, repo1Messages);

        repo1.synchronize(repo2);
        String[] correctOrdering = {"1", "2", "3"};

        assertEquals(3, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());   
        testHistory(repo1, 3, correctOrdering);
    }

    @Test
    @DisplayName("Test synchronize() - empty case (part 2)")
    public void testSynchronizeEmpty2() throws InterruptedException {
        // check if repo1 is empty and repo2 has things
        String[] repo2Messages = {"4", "5", "6"};
        commitAll(repo2, repo2Messages);

        repo1.synchronize(repo2);
        String[] correctOrdering2 = {"4", "5", "6"};

        assertEquals(3, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());   
        testHistory(repo1, 3, correctOrdering2);
    }

    @Test
    @DisplayName("Test synchronize() - end case")
    public void testSynchronizeEnd() throws InterruptedException {
        String[] repo2Messages = {"uno", "dos", "tres"};
        commitAll(repo2, repo2Messages);
        String[] repo1Messages = {"ek", "do", "teen"};
        commitAll(repo1, repo1Messages);

        repo1.synchronize(repo2);
        String[] correctOrdering = {"uno", "dos", "tres", "ek", "do", "teen"};

 
        assertEquals(6, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());   
        testHistory(repo1, 6, correctOrdering);       
    }

    /////////////////////////////////////////////////////////////////////////////////
    // PROVIDED HELPER METHODS (You don't have to use these if you don't want to!) //
    /////////////////////////////////////////////////////////////////////////////////

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. 
    // repo and messages should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    private void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. repo and allCommits should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    private void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
