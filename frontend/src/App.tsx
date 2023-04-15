import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "./App.css";
import SessionStatus from "./SessionStatus";
import useSession from "./useSession";

export default function App() {
  const queryClient = new QueryClient();
  const { session, setSession } = useSession();

  return (
    <QueryClientProvider client={queryClient}>
      <header>
        <h1>
          <a href="/">Influencer&nbsp;2</a>
        </h1>
        <SessionStatus session={session} onLogin={setSession} />
      </header>
      <main>foo</main>
      <footer>
        Source Code on <a href="https://github.com/tilltheis/influencer2">GitHub</a>
      </footer>
    </QueryClientProvider>
  );
}
