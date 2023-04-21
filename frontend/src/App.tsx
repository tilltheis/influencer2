import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "./App.css";
import DraftPostButton from "./DraftPostButton";
import Posts from "./Posts";
import { SessionProvider, useSession } from "./SessionContext";
import SessionStatus from "./SessionStatus";

export default function App() {
  return (
    <SessionProvider>
      <AppInSessionContext />
    </SessionProvider>
  );
}

function AppInSessionContext() {
  const queryClient = new QueryClient();
  const { session, setSession } = useSession();

  let headerButtons;

  // TODO: split up SessionStatus into LoginRegisterButton and LogoutButton and add them here
  if (session) {
    headerButtons = (
      <div>
        <DraftPostButton session={session} />
      </div>
    );
  } else {
    headerButtons = <></>;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <header>
        <h1>
          <a href="/">Influencer&nbsp;2</a>
        </h1>
        {headerButtons}
        <SessionStatus session={session} onLogin={setSession} onLogout={() => setSession(null)} />
      </header>
      <main>
        <Posts />
      </main>
      <footer>
        Source Code on <a href="https://github.com/tilltheis/influencer2">GitHub</a>
      </footer>
    </QueryClientProvider>
  );
}
