package sender;

import data.Buyer;
import data.Correspondence;

public record ForSender(Correspondence[] message, Buyer buyer) {}
