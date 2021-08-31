package net.jacobpeterson;

import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NextcloudUserImporter {

    private final String[] args;

    /**
     * Instantiates a new {@link NextcloudUserImporter}.
     *
     * @param args the args
     */
    public NextcloudUserImporter(String[] args) {
        this.args = args;
    }

    /**
     * Runs the user importer.
     *
     * @throws Exception thrown for {@link Exception}s
     */
    public void run() throws Exception {
        final List<Member> members = new ArrayList<>();

        System.out.printf("Running as user: %s\n", System.getProperty("user.name"));
        System.out.println("Validating CSV...");

        final Splitter quoteEscapedCommaDelimitedSplitter =
                Splitter.on(Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
        List<String> memberCSVLines = Files.readAllLines(Paths.get(args[0]));
        for (int line = 0, memberCSVLinesSize = memberCSVLines.size(); line < memberCSVLinesSize; line++) {
            List<String> memberCSV = quoteEscapedCommaDelimitedSplitter.splitToList(memberCSVLines.get(line));

            if (memberCSV.size() < 3) {
                System.err.println("Malformed CSV on line: " + line);
            }

            String firstName = memberCSV.get(0);
            String lastName = memberCSV.get(1);
            String uidEmail = memberCSV.get(2);

            if (firstName.isBlank()) {
                System.err.println("Missing first name on line: " + (line + 1));
                continue;
            } else if (lastName.isBlank()) {
                System.err.println("Missing last name on line: " + (line + 1));
                continue;
            } else if (uidEmail.isBlank()) {
                System.err.println("Missing UID email on line: " + (line + 1));
                continue;
            }

            members.add(new Member(firstName, lastName, uidEmail));
        }

        System.out.println("Valid CSV.");

        final String pathToNextcloudOCC = new File(args[1]).getAbsolutePath();
        for (Member member : members) {
            final String memberUsername = (member.getFirstName().toLowerCase() + member.getLastName().toLowerCase())
                    .replace(" ", "");
            final String memberGroup = "Member";

            // Add the user

            List<String> addUserCommand = new ArrayList<>();
            addUserCommand.add("php");
            addUserCommand.add(pathToNextcloudOCC);
            addUserCommand.add("user:add");

            addUserCommand.add(String.format("--display-name=%s",
                    Stream.of(member.getFirstName(), member.getLastName())
                            .flatMap(s -> Arrays.stream(s.split(" ")))
                            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "))));
            addUserCommand.add(String.format("--group=%s", memberGroup));
            addUserCommand.add("--password-from-env");
            addUserCommand.add(memberUsername);

            ProcessBuilder addUserProcessBuilder = new ProcessBuilder();
            addUserProcessBuilder.redirectErrorStream(true);
            addUserProcessBuilder.command(addUserCommand);
            addUserProcessBuilder.environment().put("OC_PASS", memberUsername + "123");

            System.out.printf("Executing add user command: %s\n", String.join(" ", addUserCommand));
            Process addUserProcess = addUserProcessBuilder.start();
            System.out.printf("Output of add user command: %s\n",
                    CharStreams.toString(new InputStreamReader(addUserProcess.getInputStream())));

            // Now set user email

            List<String> setUserEmailCommand = new ArrayList<>();
            setUserEmailCommand.add("php");
            setUserEmailCommand.add(pathToNextcloudOCC);
            setUserEmailCommand.add("user:setting");
            setUserEmailCommand.add(memberUsername);
            setUserEmailCommand.add("settings");
            setUserEmailCommand.add("email");
            setUserEmailCommand.add(member.getUIDEmail());

            ProcessBuilder setUserEmailProcessBuilder = new ProcessBuilder();
            setUserEmailProcessBuilder.redirectErrorStream(true);
            setUserEmailProcessBuilder.command(setUserEmailCommand);

            System.out.printf("Executing set user email command: %s\n", String.join(" ", setUserEmailCommand));
            Process setUserEmailProcess = setUserEmailProcessBuilder.start();
            System.out.printf("Output of set user email command: %s\n",
                    CharStreams.toString(new InputStreamReader(setUserEmailProcess.getInputStream())));
        }

        System.out.println("Done.");
    }

    /**
     * Represents a Nextcloud member.
     */
    static class Member {
        private final String firstName;
        private final String lastName;
        private final String uidEmail;

        /**
         * Instantiates a new {@link Member}.
         *
         * @param firstName the first name
         * @param lastName  the last name
         * @param uidEmail  the UID email
         */
        public Member(String firstName, String lastName, String uidEmail) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.uidEmail = uidEmail;
        }

        /**
         * Gets first name.
         *
         * @return the first name
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * Gets last name.
         *
         * @return the last name
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * Gets uid email.
         *
         * @return the uid email
         */
        public String getUIDEmail() {
            return uidEmail;
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws Exception {
        NextcloudUserImporter nextcloudUserImporter = new NextcloudUserImporter(args);
        nextcloudUserImporter.run();
    }
}
