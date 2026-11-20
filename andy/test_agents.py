import unittest
from research_agents import Advisory, AndroidAgent, APIAgent, IOSAgent, GeneralAgent

class AgentRoutingTests(unittest.TestCase):
    def setUp(self):
        self.items = [
            Advisory("test", "CVE-1", "Android kernel issue", "A flaw", "high", "", "", "https://example.org/1", ("Android",)),
            Advisory("test", "CVE-2", "OAuth token validation issue", "API auth", "medium", "", "", "https://example.org/2", ("REST API",)),
            Advisory("test", "CVE-3", "Apple WebKit issue", "Browser flaw", "high", "", "", "https://example.org/3", ("WebKit",)),
        ]
    def test_android_agent(self):
        self.assertEqual([x.identifier for x in AndroidAgent().review(self.items)], ["CVE-1"])
    def test_api_agent(self):
        self.assertEqual([x.identifier for x in APIAgent().review(self.items)], ["CVE-2"])
    def test_ios_agent(self):
        self.assertEqual([x.identifier for x in IOSAgent().review(self.items)], ["CVE-3"])
    def test_general_agent_returns_all(self):
        self.assertEqual(len(GeneralAgent().review(self.items)), 3)

if __name__ == "__main__":
    unittest.main()
