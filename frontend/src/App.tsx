import { useState } from "react";
import "./App.css";
import LoginStatus from "./LoginStatus";

export default function App() {
  const [count, setCount] = useState(0);

  return (
    <>
      <header>
        <h1>
          <a href="/">Influencer&nbsp;2</a>
        </h1>
        <LoginStatus />
      </header>
      <main>foo</main>
      <footer>
        Source Code on{" "}
        <a href="https://github.com/tilltheis/influencer2">GitHub</a>
      </footer>
    </>
  );
}
