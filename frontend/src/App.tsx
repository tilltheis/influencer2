import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "./App.css";
import Header from "./Header";
import Posts from "./Posts";
import { SessionProvider } from "./SessionContext";

export default function App() {
  return (
    <SessionProvider>
      <AppInSessionContext />
    </SessionProvider>
  );
}

function AppInSessionContext() {
  const queryClient = new QueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      <Header />
      <main>
        <Posts />
      </main>
      <footer>
        Source Code on <a href="https://github.com/tilltheis/influencer2">GitHub</a>
      </footer>
    </QueryClientProvider>
  );
}
