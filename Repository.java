// Tisya Bhatia
// 2.12.2925
// CSE 123 BB
// P1: Mini Git
// TA: Shreya Pandey

import java.util.*;
import java.text.SimpleDateFormat;

/*
 * This class called Repository represents a traditional repository for users to essentially track
 * changes (like in a Google Docs, for example). There are a few key usability features: users can 
 * create a new Repository (which stores individual commits), create new commits, retrieve the size
 * of their repo and most recent commit made to the repo, see as far back in their history as they
 * want to, remove a certain commit from their repository, and merge two Repos while still 
 * maintaining the timestamp ordering of the two original repositories. 
*/
public class Repository {
    // private fields 
    private Commit head;
    private String name;

    /*
     * Creates a new Repository
     * Parameters: String name of new Repository
     * Exceptions: IllegalArgumentException if the Repository name given is null or empty
     */
    public Repository (String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The given Repository name is null or empty");
        }

        this.head = null;
        this.name = name;
    }


    /*
     * Provides the id of the most recently added commit. Null if no commits in Repository
     * Returns: String id of the most recent commit or null if no commits in Repository
     */
    public String getRepoHead() {
        if (this.head == null) {
            return null;
        }

        return this.head.id;
    }

    /*
     * Provides the number of commits in the Repository
     * Returns: int number of commits in the Repository
     */
    public int getRepoSize() {
        int commits = 0;
        Commit curr = this.head;
        while (curr != null) {
            commits++;
            curr = curr.past;
        }

        return commits;
    }

    /*
     * Provides the user with a text representation of the Repository:
     *    => if the repo has no commits, then will provide name and " - no Commits" statement
     *    => otherwise, will provide "<name of repo> - Current head: <most recent commit>"
     *       (commit is provided as a text representation of commitwith id, timestamp, and message)
     * Returns: String representation of Repository
     */
    public String toString() {
        if (getRepoSize() == 0) {
            return this.name + " - No commits";
        }

        return this.name + " - Current head: " + this.head.toString();
    }
    
    /*
     * Provides information if the Repository contains a commit with a certain id (true or false)
     * Parameters: String targetId which is the id to search for in the Repo
     * Returns: boolean true if Repo contains commit with targetId, false if not
     * Exceptions: throws IllegalArgumentException if the targetId given by user is null
     */
    public boolean contains(String targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("targetId is null");
        }

        Commit curr = this.head;
        while (curr != null) {
            if (curr.id.equals(targetId)) {
                return true;
            }

            curr = curr.past;
        }

        return false;
    }

    /*
     * Provides user with text based representation of latest n commits
     *   => if n is greater than num of commits in Repo, will show all commits
     *   => if there are no commits in the repo, provide an empty text string
     * Parameters: int n representing the number of most recent commits to return
     * Returns: a String based representation of the latest n commits separated by nextLine
     * Exceptions: throws IllegalArgumentException if n is nonpositive
     */
    public String getHistory(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("given int n for getHistory was non-positive");
        }

        String result = "";
        int index = 0;
        Commit curr = this.head;

        while (curr != null && index < n) {
            result += curr.toString() + "\n";
            curr = curr.past;
            index++;
        }

        return result;
    }

    /*
     * Creates a new commit in the Repo w/ given message
     * Parameters: String message which is the commit's message
     * Returns: String representing the id of the newly created commit
     * Exceptions: throws IllegalArgumentException if the message for the commit is null
     */
    public String commit(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message for the commit() method param is null");
        }

        this.head = new Commit(message, this.head);

        return this.head.id;
    }

    /*
     * Removes the commit with the target id from the repo while maintaining the rest of history;
     * provides the user with true/ false info on whether the commit was dropped/ not found in repo
     * Parameters: String targetID which the id to look for in repo and remove form repo if found
     * Returns: a boolean representing whether the commit was succesfully removed or not found
     * Exceptions: throws an IllegalArgumentException if the targetId given is null 
     */
    public boolean drop(String targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("targetId parameter for drop() method is null");
        }

        if (this.head != null) {
            if (this.head.id.equals(targetId)) {
                this.head = this.head.past;
                return true;
            }

            Commit curr = this.head;
            while (curr.past != null) {
                if (curr.past.id.equals(targetId)) {
                    curr.past = curr.past.past;
                    return true;
                }
                
                curr = curr.past;
            }
        }

        return false;
    }

    /*
     * Takes all commits from the given other repo and moves them into current repo. Combines 
     * in such a way so that the commits in the repo is still organized by timestamps 
     * Parameters: Repository other to chronologically move to the current Repo
     * Exceptions: if the other repository is null, throws an IllegalArgumentException
     */
    public void synchronize(Repository other) {
        if (other == null) {
            throw new IllegalArgumentException("other repository is null; synchronize()");
        }

        // comments are notes for future reference:

        // if THIS repo has NO COMMITS (i.e. null or empty)
        if (this.head == null || this.getRepoSize() == 0) {
            this.head = other.head; // this repo head refers to other's repo head

            // if THIS repo HAS COMMITS (i.e. nonnull or non-empty)
        } else if (other.head != null) {
            // front vase: if other head is bigger than current head, add other node to the front
            if (this.head.timeStamp < other.head.timeStamp) {
                Commit temp = this.head;
                this.head = other.head;
                this.head.past = other.head.past;
                other.head = temp;
                // temp = other.head.past;
            }

            Commit curr1 = this.head;

            // middle case: check if a node is less than the curent one but greater than the next one
            while (curr1 != null && other.head != null && curr1.past != null) {
                if (curr1.timeStamp > other.head.timeStamp && other.head.timeStamp > curr1.past.timeStamp) {
                    Commit temp = curr1.past;
                    curr1.past = other.head;
                    other.head = other.head.past;
                    curr1.past.past  = temp;

                } 

                System.out.println(this.head.toString());
                
                curr1 = curr1.past;

            }

            // end case
            if (curr1.past == null && other.head != null) {
                if (other.head.timeStamp > curr1.timeStamp) {
                    curr1 = other.head;
                } else {
                    curr1.past = other.head;
                }
            }
        }

        other.head = null; // end with the other repo being null (in spec)
    }

    /**
     * DO NOT MODIFY
     * A class that represents a single commit in the repository.
     * Commits are characterized by an identifier, a commit message,
     * and the time that the commit was made. A commit also stores
     * a reference to the immediately previous commit if it exists.
     *
     * Staff Note: You may notice that the comments in this 
     * class openly mention the fields of the class. This is fine 
     * because the fields of the Commit class are public. In general, 
     * be careful about revealing implementation details!
     */
    public static class Commit {

        private static int currentCommitID;

        /**
         * The time, in milliseconds, at which this commit was created.
         */
        public final long timeStamp;

        /**
         * A unique identifier for this commit.
         */
        public final String id;

        /**
         * A message describing the changes made in this commit.
         */
        public final String message;

        /**
         * A reference to the previous commit, if it exists. Otherwise, null.
         */
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        /**
        * Resets the IDs of the commit nodes such that they reset to 0.
        * Primarily for testing purposes.
        */
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}

