import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "./App.css";
import Header from "./Header";
import Posts from "./Posts";
import { SessionProvider } from "./SessionContext";
import { Outlet, RouterProvider, createBrowserRouter, useParams } from "react-router-dom";
import User from "./User";

const queryClient = new QueryClient();
const router = createBrowserRouter([
  {
    path: "/",
    Component: Root,
    children: [
      { path: "/", Component: Posts },
      {
        path: "/:username",
        Component: function () {
          const { username } = useParams();
          if (!username) throw new Error("missing param :username"); // impossible for required param
          return <User username={username} />;
        },
      },
    ],
  },
]);

export default function App() {
  return (
    <SessionProvider>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </SessionProvider>
  );
}

function Root() {
  return (
    <>
      <Header />
      <main>
        <Outlet />
      </main>
      <footer>
        Source Code on <a href="https://github.com/tilltheis/influencer2">GitHub</a>
      </footer>
    </>
  );
}
