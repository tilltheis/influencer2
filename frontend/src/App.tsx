import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "./App.css";
import DraftPostButton from "./DraftPostButton";
import LoginRegisterButton from "./LoginRegisterButton";
import LogoutButton from "./LogoutButton";
import Posts from "./Posts";
import { SessionProvider, useSession } from "./SessionContext";

export default function App() {
  return (
    <SessionProvider>
      <AppInSessionContext />
    </SessionProvider>
  );
}

function AppInSessionContext() {
  const queryClient = new QueryClient();
  const { session } = useSession();

  let headerButtons;

  if (session) {
    headerButtons = (
      <>
        <DraftPostButton />
        <LogoutButton />
      </>
    );
  } else {
    headerButtons = (
      <>
        <LoginRegisterButton />
      </>
    );
  }

  return (
    <QueryClientProvider client={queryClient}>
      <header>
        <h1>
          <a href="/">Influencer&nbsp;2</a>
        </h1>
        {headerButtons}
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
