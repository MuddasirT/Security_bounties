"""Passive public-advisory agents. No target scanning or exploitation."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable

@dataclass(frozen=True)
class Advisory:
    source: str
    identifier: str
    title: str
    summary: str
    severity: str
    published: str
    updated: str
    url: str
    products: tuple[str, ...] = ()

class SpecialistAgent:
    name = "specialist"
    keywords: tuple[str, ...] = ()

    def review(self, advisories: Iterable[Advisory]) -> list[Advisory]:
        terms = tuple(x.lower() for x in self.keywords)
        found = []
        for item in advisories:
            haystack = " ".join((item.title, item.summary, " ".join(item.products))).lower()
            if any(term in haystack for term in terms):
                found.append(item)
        return found

class AndroidAgent(SpecialistAgent):
    name = "Android Security Agent"
    keywords = ("android", "pixel", "google android", "aosp", "android kernel")

class IOSAgent(SpecialistAgent):
    name = "iOS Security Agent"
    keywords = ("ios", "ipados", "iphone", "ipad", "webkit", "xcode", "apple")

class WebAgent(SpecialistAgent):
    name = "Web Security Agent"
    keywords = ("http", "https", "nginx", "apache", "tomcat", "wordpress", "next.js", "react", "vue", "web application")

class APIAgent(SpecialistAgent):
    name = "API Security Agent"
    keywords = ("api", "graphql", "oauth", "openid", "jwt", "grpc", "rest")

class CloudAgent(SpecialistAgent):
    name = "Cloud and Infrastructure Agent"
    keywords = ("aws", "amazon", "azure", "google cloud", "kubernetes", "docker", "linux", "openssl", "ssh")

class GeneralAgent(SpecialistAgent):
    name = "General Vulnerability Agent"
    keywords = ()
    def review(self, advisories: Iterable[Advisory]) -> list[Advisory]:
        return list(advisories)

AGENTS = (AndroidAgent(), IOSAgent(), WebAgent(), APIAgent(), CloudAgent(), GeneralAgent())
