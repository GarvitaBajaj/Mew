public class Constants {

    //************RabbitMQ Configuration*****************
    public static String SERVER_ADDRESS="localhost";
    public static String EXCHANGE_NAME="exchange.ps.resquery";
    public static String EXCHANGE_TYPE="topic";
    public static String ROUTING_KEY="queries";
    public static String QUEUE_NAME="queue.ps.queries";

    //***********Experiment Setup************************
    public static int MIN_PROVIDERS=2;
    public static int MAX_PROVIDERS=7;
    public static String PROVIDER="1921681910324041";//        "{"1b93c9dc3a9f8f9c","44792dfb2b2301b4","4a5395f3320fa158","59c30ba85acb465f","5b09a9fc25b1a66e","8d7ed6d3ee453134","98728455d9ec50cd","9d6706888645b1aa","a30c120312e2ecc9","a348016717671f24","c72706c39647bc10","d0dbd90ba2c6b816","d3b99c5c1be691d0","d5d28042b406772d"};
    public static int QUERY_DURATION=5*60*1000;    //in milliseconds
    public static int NOOFQUERIES=70;//1500;
    public static long DELAY=2*60*1000;             //start data collection 2 minutes from now
//    public static long EXPERIMENT_DURATION=10*60*60*1000;
    //public static long QUERY_TIME=EXPERIMENT_DURATION/NOOFQUERIES;
    public static long QUERY_TIME= 10*60*1000; //1 query in 10 minutes
}
